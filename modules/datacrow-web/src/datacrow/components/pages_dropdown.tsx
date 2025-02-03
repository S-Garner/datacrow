import { useState } from "react";
import { Button } from "react-bootstrap";

type DropdownProps = {
	title: string;
	options: number[];
	handleSelectOption: (option: string) => void;
};

export default function PagesDropdown({
	title,
	options,
	handleSelectOption,
}: DropdownProps) {
    
	const [isOpen, setIsOpen] = useState(false);

	const toggleDropdown = () => {
		setIsOpen(!isOpen);
	};

	return (
		<div style={{ display: "flex", flexWrap: "wrap" }} id="pageSelect">
		
			<Button onClick={toggleDropdown} key="pageDropDown" className="page-menu-button">
				<span>{title}</span>
			</Button>

			{isOpen && (
				<div style={{ display: "flex", flexWrap: "wrap" }} id="pageSelectOverview">
					{options.map((option) => (
						<Button
							className="page-button"
							key={option}
							onClick={() => {
								handleSelectOption("" + option);
								setIsOpen(false);
							}}
						>
							{option}
						</Button>
					))}
				</div>
			)}
		</div>
	);
}
